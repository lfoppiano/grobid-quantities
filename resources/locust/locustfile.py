import random

from locust import HttpUser, task, between


class QuickstartUser(HttpUser):
    wait_time = between(5, 9)

    text_documents = []

    @task
    def process_text(self):
        n = random.randint(0, len(self.text_documents) - 1)
        text_document = self.text_documents[n]
        headers = {"Accept": "application/json"}
        files = {"text": text_document}
        self.client.post("/service/processQuantityText", files=files, headers=headers, name="processQuantityText")

    def on_start(self):
        if len(self.text_documents) == 0:
            with open("testData.txt", 'r') as fp:
                lines = fp.readlines()

            self.text_documents.extend(lines)
