task dockerhub {
    command {
        echo "hello"
    }
    runtime {
        docker: "ruchim/dropseq"
    }
}

workflow docker_hash_dockerhub_private {
    call dockerhub
}
