#!/bin/sh
openssl enc -d -aes-256-cbc -d -pass env:ENC_PASS -in secured.tar.gz | tar xz