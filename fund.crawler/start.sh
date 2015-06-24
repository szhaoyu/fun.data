#!/bin/sh

export DISPLAY=:1
cd /home/node/FundsCrawler
java -jar FundCrawler-1.0-jar-with-dependencies.jar -c 3600 >> funds.log 2>&1 &
