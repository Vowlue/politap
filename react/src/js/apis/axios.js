import axios from 'axios'

const server = "http://localhost:8080"

const initiateJob = async (jobInfo, callback, errfunc) => {
    axios.post(`${server}/initiateJob`, jobInfo)
      .then(res => { callback(res.data.id) } )
      .catch(err => { errfunc(err) })
}

const deleteJob = async (id, callback, errfunc) => {
    axios.post(`${server}/deleteJob`, id)
    .then(res => { console.log(res.data) })
    .catch(err => { errfunc(err) })
}

const getStateData = async (state, callback, errfunc) => {
  axios.post(`${server}/getStateData`, state)
  .then(res => { callback(res.data) })
  .catch(err => { errfunc(err) })
}

const getDistrictings = async (id, callback, errfunc) => {
  axios.post(`${server}/getDistrictings`, id)
  .then(res => { callback(res.data) })
  .catch(err => { errfunc(err) })
}

const cancelJob = async (id, callback, errfunc) => {
  axios.post(`${server}/cancelJob`, id)
  .then(res => { callback(res.data) })
  .catch(err => { errfunc(err) })
}

export { initiateJob, deleteJob, getStateData, getDistrictings, cancelJob }