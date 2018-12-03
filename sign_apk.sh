#!/usr/bin/env bash
rm -rf ./release
mkdir ./release
STADPATH=${1:-~/Android/Sdk/build-tools/27.0.3}
echo "Standard path: ${STADPATH}"
for FILE in `find ./app/build/outputs/apk/ -name "*.apk" -type f`
do
	filename=$(basename $FILE)
	keystorePath=${2:-"./keystore/keystore.jks"}
	echo "ASSEMBLE: ${FILE} ${filename} :: keystore=${keystorePath}"
	filename="${filename%.*}"
	${STADPATH}/zipalign -v -p 4 ${FILE} ./release/${filename}-aligned.apk
	${STADPATH}/apksigner sign --ks ${keystorePath} --out ./release/${filename}-aligned-signed.apk ./release/${filename}-aligned.apk
	echo "ASSEMBLE: done ${filename}" ; ls -l ./release/${filename}-aligned-signed.apk
done
ls -l ./release
