import os

import random

from locust import HttpUser, task, between, tag


class QuickstartUser(HttpUser):
    wait_time = between(5, 9)

    text_documents = []
    # pdf_documents = []

    @tag("process_texts")
    @task
    def process_text(self):
        n = random.randint(0, len(self.text_documents) - 1)
        text_document = self.text_documents[n]
        headers = {"Accept": "application/json"}
        files = {"text": text_document}
        self.client.post("/service/processQuantityText", files=files, headers=headers, name="processQuantityText")

    # @tag('process_pdfs')
    # @task
    # def process_pdfs(self):
    #     n = random.randint(0, len(self.pdf_documents) - 1)
    #     pdf_document = self.pdf_documents[n]
    # 
    #     files = {
    #         'input': (
    #             pdf_document,
    #             open(pdf_document, 'rb'),
    #             'application/pdf',
    #             {'Expires': '0'}
    #         )
    #     }
    # 
    #     headers = {"Accept": "application/json"}
    #     self.client.post("/service/annotateQuantityPDF", files=files, headers=headers,
    #                      name="/service/annotateQuantityPDF")

    def on_start(self):
        if len(self.text_documents) == 0:
            with open("testData.txt", 'r') as fp:
                lines = fp.readlines()

            self.text_documents.extend(lines)

       #  if len(self.pdf_documents) == 0:
       #      # for root, dirs, files in os.walk("resources/data/pdfs/"):
       #      for root, dirs, files in os.walk("../dataset/original/pdf"):
       #          for file_ in files:
       #              if file_.lower().endswith(".pdf"):
       #                  abs_path = os.path.join(root, file_)
       #                  self.pdf_documents.append(abs_path)
       # 