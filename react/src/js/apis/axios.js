import axios from 'axios'

const server = "http://localhost:8080"

const initiateJob = async (jobInfo, callback, errfunc) => {
    axios.post(`${server}/initiateJob`, jobInfo)
      .then(res => { callback(res.data.id) } )
      .catch(err => { errfunc(err) })
}

const deleteJob = async (id, callback, errfunc) => {
    axios.post(`${server}/deleteJob`, {ID: id})
    .then(res => { callback(res) })
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

const getHistory = async (callback, errfunc) => {
  axios.get(`${server}/getHistory`)
  .then(res => { callback(res.data) })
  .catch(err => { errfunc(err) })
}

const getBoxPlot = async (id, callback, errfunc) => {
  axios.post(`${server}/getBoxPlot`, id)
  .then(res => { callback(res.data) })
  .catch(err => { errfunc(err) })
}

export { initiateJob, deleteJob, getStateData, getDistrictings, cancelJob, getHistory, getBoxPlot }