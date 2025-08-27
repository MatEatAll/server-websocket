docker run --name mongodb \
    -p 27017:27017 \
    -v mongo-data-noauth:/data/db \
    -d mongo