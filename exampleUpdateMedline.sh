#!/bin/bash

# Example of script that could run this app as an ephemeral job in combination with the OHDSI MedlineXMLToDatabase
# Pass your secret stuff as environment variables

DB_DIALECT=MYSQL
INI_FILE_NAME=medline_load.ini
FILES_FOLDER=files
FILES_FOLDER_PATH=$(pwd)/${FILES_FOLDER}

mkdir ${FILES_FOLDER}

echo "Creating ini file for xml to db app"
cat >${INI_FILE_NAME} <<EOF
DATA_SOURCE_TYPE = ${DB_DIALECT}
USER = ${USER}
PASSWORD = ${PASSWORD}
SCHEMA = ${DB}
CREATE_SCHEMA = false
SERVER = ${HOST}
XML_FOLDER = ${FILES_FOLDER}
MESH_XML_FOLDER = ${FILES_FOLDER_PATH}
EOF

echo "Downloading new files"
java -jar MedlineDownloader.jar

echo "Parsing XML Files"
java -jar -Xmx10G MedlineXmlToDatabase.jar -parse -ini ${INI_FILE_NAME}
