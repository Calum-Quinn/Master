# Task 3 - Add and exercise resilience

By now you should have understood the general principle of configuring, running and accessing applications in Kubernetes. However, the above application has no support for resilience. If a container (resp. Pod) dies, it stops working. Next, we add some resilience to the application.

## Subtask 3.1 - Add Deployments

In this task you will create Deployments that will spawn Replica Sets as health-management components.

Converting a Pod to be managed by a Deployment is quite simple.

  * Have a look at an example of a Deployment described here: <https://kubernetes.io/docs/concepts/workloads/controllers/deployment/>

  * Create Deployment versions of your application configurations (e.g. `redis-deploy.yaml` instead of `redis-pod.yaml`) and modify/extend them to contain the required Deployment parameters.

  * Again, be careful with the YAML indentation!

  * Make sure to have always 2 instances of the API and Frontend running. 

  * Use only 1 instance for the Redis-Server. Why?

    > We want to keep it at only one instance so that there can't be any data hazards, given that we will only have one state for our data.

  * Delete all application Pods (using `kubectl delete pod ...`) and replace them with deployment versions.

  * Verify that the application is still working and the Replica Sets are in place. (`kubectl get all`, `kubectl get pods`, `kubectl describe ...`)

## Subtask 3.2 - Verify the functionality of the Replica Sets

In this subtask you will intentionally kill (delete) Pods and verify that the application keeps working and the Replica Set is doing its task.

Hint: You can monitor the status of a resource by adding the `--watch` option to the `get` command. To watch a single resource:

```sh
$ kubectl get <resource-name> --watch
```

To watch all resources of a certain type, for example all Pods:

```sh
$ kubectl get pods --watch
```

You may also use `kubectl get all` repeatedly to see a list of all resources.  You should also verify if the application stays available by continuously reloading your browser window.

  * What happens if you delete a Frontend or API Pod? How long does it take for the system to react?
    ```
	It directly creates a new pod in a matter of seconds and then properly terminates the deleted one.
	```
    
  * What happens when you delete the Redis Pod?
	```
	It terminates the current pod and then very quickly creates a new one.
	```
    
  * How can you change the number of instances temporarily to 3? Hint: look for scaling in the deployment documentation
	```
	You can use a command similar to kubectl scale deployment/api-deploy --replicas=10
	```
    
  * What autoscaling features are available? Which metrics are used?
	```
	Minimum and maximum number of pods desired based on CPU usage of the current pods.
	```
    
  * How can you update a component? (see "Updating a Deployment" in the deployment documentation)
	```
	Update the necessary elements (e.g. kubectl set image deployment.v1.apps/nginx-deployment nginx=nginx:1.16.1)
	
	Rollout the changes
	```

## Subtask 3.3 - Put autoscaling in place and load-test it

On the GKE cluster deploy autoscaling on the Frontend with a target CPU utilization of 30% and number of replicas between 1 and 4. 
`kubectl autoscale deployment/frontend-deploy --min=1 --max=4 --cpu-percent=30`


Load-test using Vegeta (500 requests should be enough).
`echo "GET http://34.65.7.59" | vegeta attack -duration=1m -rate=500 | vegeta report --type=text`

```
[OUTPUT]
Requests      [total, rate, throughput]         30000, 500.01, 499.40
Duration      [total, attack, wait]             1m0s, 59.998s, 73.84ms
Latencies     [min, mean, 50, 90, 95, 99, max]  16.568ms, 102.037ms, 69.076ms, 144.631ms, 368.123ms, 604.963ms, 1.438s
Bytes In      [total, mean]                     18930000, 631.00
Bytes Out     [total, mean]                     0, 0.00
Success       [ratio]                           100.00%
Status Codes  [code:count]                      200:30000
Error Set:
```

```
kubectl get deployment --watch

[OUTPUT]
NAME              READY   UP-TO-DATE   AVAILABLE   AGE
api-deploy        2/2     2            2           4h55m
frontend-deploy   2/2     2            2           4h55m
redis-deploy      1/1     1            1           4h55m
frontend-deploy   2/2     2            2           5h2m
frontend-deploy   2/2     2            2           5h2m
frontend-deploy   2/2     0            2           5h2m
frontend-deploy   2/2     1            2           5h2m
frontend-deploy   3/2     1            3           5h2m
frontend-deploy   2/2     1            2           5h2m
frontend-deploy   2/2     2            2           5h2m
frontend-deploy   3/2     2            3           5h2m
frontend-deploy   2/2     2            2           5h2m
frontend-deploy   2/4     2            2           5h3m
frontend-deploy   2/4     2            2           5h3m
frontend-deploy   2/4     2            2           5h3m
frontend-deploy   2/4     4            2           5h3m
frontend-deploy   3/4     4            3           5h3m
frontend-deploy   4/4     4            4           5h3m
frontend-deploy   4/1     4            4           5h9m
frontend-deploy   4/1     4            4           5h9m
frontend-deploy   1/1     1            1           5h9m
```

> [!NOTE]
>
> - The autoscale may take a while to trigger.
>
> - If your autoscaling fails to get the cpu utilization metrics, run the following command
>
>   - ```sh
>     $ kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
>     ```
>
>   - Then add the *resources* part in the *container part* in your `frontend-deploy` :
>
>   - ```yaml
>     spec:
>       containers:
>         - ...:
>           env:
>             - ...:
>           resources:
>             requests:
>               cpu: 10m
>     ```
>

## Deliverables

Document your observations in the lab report. Document any difficulties you faced and how you overcame them. Copy the object descriptions into the lab report.

> // TODO

<<<<<<< HEAD
```````sh
// TODO autoscaling description
=======
```txt
Again, we didn't really encounter difficulties. Things went well.

We did observe that the autoscaling was working as expected via the vegeta tests. Things were kept stable.
```

>>>>>>> 64734668e6368991f75feccc9c2a407bb5a1d231
```yaml
# redis-deploy.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis-deploy
  labels:
    component: redis
    app: todo
spec:
  replicas: 1
  selector:
    matchLabels:
      component: redis
      app: todo
  template:
    metadata:
      labels:
        component: redis
        app: todo
    spec:
      containers:
      - name: redis
        image: redis
        ports:
        - containerPort: 6379
        args:
        - redis-server
        - --requirepass ccp2
        - --appendonly yes

```

```yaml
# api-deploy.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-deploy
  labels:
    component: api
    app: todo
spec:
  replicas: 2
  selector:
    matchLabels:
      component: api
      app: todo
  template:
    metadata:
      labels:
        component: api
        app: todo
    spec:
      containers:
      - name: api
        image: icclabcna/ccp2-k8s-todo-api
        ports:
        - containerPort: 8081
        env:
        - name: REDIS_ENDPOINT
          value: redis-svc
        - name: REDIS_PWD
          value: ccp2

```

```yaml
# frontend-deploy.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: frontend-deploy
  labels:
    component: frontend
    app: todo
spec:
  replicas: 2
  selector:
    matchLabels:
      component: frontend
      app: todo
  template:
    metadata:
      labels:
        component: frontend
        app: todo
    spec:
      containers:
      - name: frontend
        image: icclabcna/ccp2-k8s-todo-frontend
        ports:
        - containerPort: 8080
        env:
        - name: API_ENDPOINT_URL
          value: "http://api:8081"
```
