#/bin/sh
tar -czf - src/test/resources/edm.xml | openssl enc -aes-256-cbc -pass env:ENC_PASS -out secured.tar.gz