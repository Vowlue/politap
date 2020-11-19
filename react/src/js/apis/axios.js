import axios from 'axios'

const initiateJob = async jobInfo => {
    axios.post(`http://localhost:8080/initiateJob`, jobInfo)
      .then(res => res.data.id)
}