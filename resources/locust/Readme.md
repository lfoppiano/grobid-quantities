# Performance test for grobid-quantities

> conda create --name locust python=3.9 pip 

> pip install locust 

> cd resources/locust 

Start the master 
> locust --config master.conf 

Start the worker (in this example we need at least one worker)
> locust --worker 

Connect to the locust interface on http://localhost:8089

You can run the locust without interface by 
1. set headless = True in the master.conf 
2. specify an output html file ``--output output.html`` when you start the master
    > locust --config master.conf 
