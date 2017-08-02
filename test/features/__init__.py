from aloe import step, world, before, after
from nose.tools import assert_equals, assert_in, assert_is_none
from subprocess import call
import requests, os, time

docker_ip = os.environ.get("DOCKER_IP") if os.environ.get("DOCKER_IP") else "127.0.0.1"
BASE_URL = "http://{}:8001".format(docker_ip)


def start_application():
    # call("./rundocker.sh")

    max_tries = 30
    try_num = 0
    connected = False
    while not connected:
        if try_num == max_tries:
            raise Exception("Connection with server timed out")
        try:
            requests.get("{}/ping".format(BASE_URL))
            connected = True
        except requests.exceptions.ConnectionError:
            time.sleep(2)  # wait for server warmup
        try_num += 1


def stop_application():
    # call("./stopdocker.sh")
    call("./restartdocker.sh")


@before.each_example
def before_each(scenario, outline, steps):
    world.resp = None
    world.jwt = None
    # stop_application()
    time.sleep(3)  # wait for server cooldown
    start_application()


@after.each_example
def after_each(scenario, outline, steps):
    world.resp = None
    stop_application()

#
# Given
#
@step(r"the service is running")
def check_server_running(step):
    resp = requests.get("{}/ping".format(BASE_URL))
    assert_equals(200, resp.status_code)


@step(r"the user '(.*)' is logged in with password '(.*)'")
def logged_user(step, email, password):
    user = {
        "email": email,
        "password": password
    }
    resp = requests.post("{}/user/login".format(BASE_URL), json=user)
    data = resp.json().get("data")
    world.jwt = data.get("jwt")
    assert_in("jwt", data)


@step(r"there's '(.*)' unit\(s\) of a product with id '(.*)' in the shopping cart")
def products_in_cart(step, qty, product_id):
    headers = {"Authorization": "Bearer {}".format(world.jwt)}
    payload = {
        "qty": int(qty)
    }
    resp = requests.put("{}/user/cart/{}".format(BASE_URL, int(product_id)), headers=headers, json=payload)
    assert_equals(200, resp.status_code)


@step(r"there's '(.*)' unit\(s\) of a product with id '(.*)' in stock")
def check_product_in_stock(step, qty, product_id):
    resp = requests.get("{}/product/{}".format(BASE_URL, int(product_id)))
    assert_equals(200, resp.status_code)
    data = resp.json().get("data")
    product = data.get("product")
    assert_equals(product.get("qty"), int(qty))


#
# When
#
@step(r"I retrieve the user '(?P<email>.*)' with the password '(?P<password>.*)'")
def login_user(step, email, password):
    user = {
        "email": email,
        "password": password
    }
    world.resp = requests.post("{}/user/login".format(BASE_URL), json=user)


@step(r"I try to log out")
def logout_user(step):
    headers = {"Authorization": "Bearer {}".format(world.jwt)}
    world.resp = requests.post("{}/user/logout".format(BASE_URL), headers=headers)


@step(r"I try to retrieve all products")
def get_products(step):
    world.resp = requests.get("{}/products".format(BASE_URL))


@step(r"I try to retrieve a product with id '(.*)'")
def get_product(step, product_id):
    world.resp = requests.get("{}/product/{}".format(BASE_URL, product_id))


@step(r"I try to get the shopping cart listing")
def get_cart_listing(step):
    headers = {"Authorization": "Bearer {}".format(world.jwt)}
    world.resp = requests.get("{}/user/cart".format(BASE_URL), headers=headers)


@step(r"I try to add '(.*)' unit\(s\) of a product with the id '(.*)' to shopping cart")
def add_to_cart(step, qty, product_id):
    headers = {"Authorization": "Bearer {}".format(world.jwt)}
    payload = {
        "qty": int(qty)
    }
    world.resp = requests.put("{}/user/cart/{}".format(BASE_URL, int(product_id)), headers=headers, json=payload)


@step(r"I try to remove a product with id '(.*)' from the shopping cart")
def remove_from_cart(step, product_id):
    headers = {"Authorization": "Bearer {}".format(world.jwt)}
    world.resp = requests.delete("{}/user/cart/{}".format(BASE_URL, int(product_id)), headers=headers)


@step(r"I try to increase '(.*)' unit\(s\) of a product with id '(.*)' in the shopping cart")
def increase_product_in_cart(step, qty, product_id):
    headers = {"Authorization": "Bearer {}".format(world.jwt)}
    payload = {
        "qty": int(qty)
    }
    world.resp = requests.post("{}/user/cart/{}/increase".format(BASE_URL, int(product_id)), headers=headers, json=payload)


@step(r"I try to decrease '(.*)' unit\(s\) of a product with id '(.*)' in the shopping cart")
def decrease_product_in_cart(step, qty, product_id):
    headers = {"Authorization": "Bearer {}".format(world.jwt)}
    payload = {
        "qty": int(qty)
    }
    world.resp = requests.post("{}/user/cart/{}/decrease".format(BASE_URL, int(product_id)), headers=headers, json=payload)


@step(r"I try to checkout")
def do_checkout(step):
    headers = {"Authorization": "Bearer {}".format(world.jwt)}
    world.resp = requests.post("{}/user/checkout".format(BASE_URL), headers=headers)


#
# Then
#
@step(r"I should get a '(.*)' response")
def check_response_code(step, expected_response_code):
    print(world.resp.content)
    assert_equals(world.resp.status_code, int(expected_response_code))


@step(r"the response must contain '(.*)'")
def check_response_contains(step, expected_json_field):
    data = world.resp.json()
    assert_in("data", data)
    assert_in(expected_json_field, data.get("data"))


@step(r"there should be '(.*)' unit\(s\) of product with id '(.*)' in the shopping cart")
def check_product_qty(step, qty, product_id):
    data = world.resp.json().get("data")
    products = data.get("cart").get("products")
    product = products.get(product_id)
    assert_equals(product.get("qty"), int(qty))


@step(r"the product with id '(.*)' in stock should have '(.*)' unit\(s\) remaining")
def check_product_qty_in_stock(step, product_id, qty):
    resp = requests.get("{}/product/{}".format(BASE_URL, int(product_id)))
    assert_equals(200, resp.status_code)
    data = resp.json().get("data")
    product = data.get("product")
    assert_equals(product.get("qty"), int(qty))


@step(r"the cart products field should be empty")
def check_empty_cart_products(step):
    headers = {"Authorization": "Bearer {}".format(world.jwt)}
    world.resp = requests.get("{}/user/cart".format(BASE_URL), headers=headers)
    data = world.resp.json().get("data")
    products = data.get("cart").get("products")
    assert_equals(products, {})
