

server:
		cd flagma-server && ./gradlew build

start-local:
		docker-compose start centraldogma grafana prometheus

stop-local:
		docker-compose stop centraldogma grafana prometheus
