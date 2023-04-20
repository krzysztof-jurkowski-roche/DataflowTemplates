all: build push

projectId ?= gcp-ent-b-msasnttst-req0000248
region ?= europe-west1
bucketName ?= dlp-data-input
templateName ?= Cloud_PubSub_to_GCS_Text_Flex
module ?= googlecloud-to-elasticsearch

build:
	mvn clean package -pl v2/googlecloud-to-elasticsearch -am

push:
	mvn clean package -PtemplatesStage  \
      -DskipTests \
      -DprojectId="$(projectId)" \
      -DbucketName="$(bucketName)" \
      -DstagePrefix="images/$(shell date +%Y_%m_%d)_01" \
      -DtemplateName="$(templateName)" \
      -pl v2/$(module) -am

run:
	mvn clean package -PtemplatesRun \
      -DskipTests \
      -DprojectId="$(projectId)" \
      -DbucketName="$(bucketName)" \
      -Dregion="$(region)" \
      -DtemplateName="$(templateName)" \
      -Dparameters="inputTopic=projects/$(projectId)/topics/{topicName},windowDuration=15s,outputDirectory=gs://{outputDirectory}/out,outputFilenamePrefix=output-,outputFilenameSuffix=.txt" \
      -pl v2/$(module) -am