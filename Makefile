-include .env
-include .env.local
export

all: clean build push

clean:
	mvn clean

build:
	mvn package -pl v2/googlecloud-to-elasticsearch -am

push:
	mvn package -PtemplatesStage  \
      -DskipTests \
      -DprojectId="$(PROJECT_ID)" \
      -DbucketName="$(BUCKET_NAME)" \
      -DstagePrefix="images/$(shell date +%Y_%m_%d)_01" \
      -DtemplateName="$(TEMPLATE_NAME)" \
      -pl v2/$(MODULE) -am

run:
	mvn package -PtemplatesRun \
      -DskipTests \
      -DprojectId="$(PROJECT_ID)" \
      -DbucketName="$(BUCKET_NAME)" \
      -Dregion="$(REGION)" \
      -DjobName="$(JOB_NAME)" \
      -DtemplateName="$(TEMPLATE_NAME)" \
      -Dparameters="inputSubscription=$(INPUT_SUBSCRIPTION),errorOutputTopic=$(ERROR_OUTPUT_TOPIC),connectionUrl=$(CONNECTION_URL),apiKey=$(API_KEY),propertyAsIndex=$(PROPERTY_AS_INDEX),propertyAsId=$(PROPERTY_AS_ID)" \
      -pl v2/$(MODULE) -am