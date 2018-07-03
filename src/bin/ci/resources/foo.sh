#!/usr/bin/env bash

docker run -it --rm -v $HOME:/root broadinstitute/dsde-toolbox vault write secret/dsde/cromwell/common/cromwell-dockerhub \
  'account=dockerhub@broadinstitute.org' \
  'auth=service_account' \
  'key_name=projects/broad-dsde-cromwell-dev/locations/global/keyRings/tj-test-private-dockerhub/cryptoKeys/tj-test-papi' \
  'password=8e7884p272g188uh749nu81ve99c28i4' \
  'token=ZmlyZWNsb3VkOmNjNDFNeHlo' \
  "token_comment=The token is ONLY for pulling private docker images. Unlike 'dsdejenkins', 'firecloud' cannot push to all private repos." \
  'token_password=cc41Mxyh' \
  'token_username=firecloud' \
  'username=dsdejenkins'
