MedLine Update Downloader
====================

This is a very simple app to automate the downloading of daily update files from the MedLine ftp server

Requirements
====================

- Java 11+
- Maven

How to run
====================

Before starting, list the update files that have already been processed in a file, or create an empty file. There is an
example existingFiles.txt available in this repo

To build the app run `mvn clean package assembly:single`

Then to
run: `java -jar target/MedlineDownloader-1.0-SNAPSHOT-jar-with-dependencies.jar <path to file with already downloaded files> <folder to store dowloaded files>`
The app takes two arguments, first is the location of the file with processed downloads and second the location you wish
to store your downloads.

After downloading each new MedLine update file it is appended to the list and won't be downloaded next time

In case you are running this as part of some cloudy ephemeral job, there is also support to store and retrieve your list
of processed files in AWS S3. In this case run the app without args and set the following four environment variables:
S3_BUCKET S3_FILENAME AWS_ID AWS_SECRET

The downloaded files are then stored locally, you will need to figure out yourself what you want to do with them next,
either alter this code or run a consecutive job like the OHDSI MedlineXmlToDatabase to further process the files :-)

Contributing
====================

Contributions and suggestions are welcome!
