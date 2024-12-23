all: docker run-docker
	echo "fine!"

dev:
	 ./gradlew --console=plain quarkusDev

docker:
	#sdk default java  22.1.0.1.r17-gln
	quarkus build --native  --no-tests
	docker build --no-cache -f src/main/docker/Dockerfile.native -t golap2docker/realtime-websocket  .
	#docker push golap2docker/realtime-websocket
	docker save golap2docker/realtime-websocket > realtime-websocket.tar
	scp realtime-websocket.tar philip@192.168.2.250:/data/upload/



run-docker:

	docker rm -f real-time-message-service
	docker run -d --memory 50m --name real-time-message-service --net host doublechaintech/real-time-message-service
	docker logs real-time-message-service
