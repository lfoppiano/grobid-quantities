import random

from locust import HttpUser, task, between

import json


class QuickstartUser(HttpUser):
    wait_time = between(5, 9)

    test_data = []

    @task
    def process_text(self):
        n = random.randint(0, len(self.test_data) - 1)
        paragraph = self.test_data[n]
        headers = {"Accept": "application/json"}
        files = {'text': str(paragraph)}
        self.client.post(url="/quantities/service/processQuantityText", files=files,
                         headers=headers, name="quantity_text")

    # @task(3)
    # def view_item(self):
    #     # item_id = random.randint(1, 10000)
    #     # self.client.get(f"/item?id={item_id}", name="/item")
    #     pass
    #
    def on_start(self):
        with open("testData.txt", 'r') as fp:
            lines = fp.readlines()

            self.test_data.extend(lines)
