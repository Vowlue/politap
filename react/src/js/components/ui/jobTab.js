import { Button, Select, Input, Header, Icon, Dropdown, Form, Label, Divider, Message, Ref} from 'semantic-ui-react'
import { generateSelection, createCheckbox } from '../jsxHelper.js'
import { useState } from 'react'

const servers = ['Local', 'Seawulf']
const censusCategories = ['White', 'Black or African American', 'American Indian or Alaska Native', 'Asian', 'Native Hawaiian or Other Pacific Islander', 'Other']
const compactness = ['Not', 'Somewhat', 'Very', 'Extremely']

function JobTab(props) {
    const jobInfo = {
        plans: 5000,
        populationVariance: 1000,
        compactness: 'Somewhat',
        server: 'Local',
        groups: [],
    }

    const [compactRef, setCompactRef] = useState(null) 
    const [serverRef, setServerRef] = useState(null)

    const setPlans = (e) => {jobInfo.plans = e.target.value}
    const setPopulationVariance = (e) => {jobInfo.populationVariance = e.target.value}
    const setCompactness = () => {setTimeout(() => {
      let c = compactRef.firstChild.innerHTML
      jobInfo.compactness = c}, 10)}
    const setServer = () => {setTimeout(() => {
      let s = serverRef.firstChild.innerHTML
      jobInfo.server = s}, 10)}
    const setGroup = (label) => {setTimeout(() => {
      const index = jobInfo.groups.indexOf(label);
      if (index > -1) {
        jobInfo.groups.splice(index, 1);
      }
      else {
        jobInfo.groups.push(label)
      }}, 10)}
    const sendJob = () => {
      jobInfo.status = "Started"
      props.addJobToHistory(jobInfo)
    }

    return (
        <Form>
          <Message>
            <Header textAlign='center' as='h2'>
              Job Configuration
              <Header.Subheader>
              Configure your own job to run.
              </Header.Subheader>
            </Header>
          </Message>
          <Divider horizontal>
            <Header as='h4'>
              <Icon name='setting' />
              Configuration
            </Header>
          </Divider>
          <Form.Field>
            <Input type='number' labelPosition='left' placeholder={jobInfo.plans}>
              <Label color='blue'># of Plans</Label>
              <input min='1' onChange={setPlans.bind(this)}/>
            </Input>
          </Form.Field>
          <Form.Field>
            <Input type='number' labelPosition='left' placeholder={jobInfo.populationVariance}>
              <Label color='blue'>Population Variance</Label>
              <input min='1' onChange={setPopulationVariance.bind(this)}/>
            </Input>
          </Form.Field>
          <Form.Field inline>
            <Label color='blue' pointing='right' size='large'>Compactness</Label>
            <Ref innerRef={setCompactRef}>
              <Select onChange={setCompactness} placeholder={jobInfo.compactness} options={generateSelection(compactness)} />
            </Ref>
          </Form.Field>
          <Form.Field>
            <Label color='blue' size='large'>Racial / Ethnic Groups</Label>
          </Form.Field>
          {censusCategories.map(category => createCheckbox(category, setGroup))}
          <Form.Field>
            Run this job on the{' '}
            <Ref innerRef={setServerRef}>
              <Dropdown floating inline options={generateSelection(servers)} onChange={setServer} defaultValue={jobInfo.server} />
            </Ref>
            {' '}server.
          </Form.Field>
          {
            props.activeState ? 
            <div>
              <Button onClick={sendJob} color='blue'>Generate Job
              </Button>
              <Label color='blue' basic content={props.jobLabelContent}>
              </Label>
            </div>
            : 
            <Button disabled basic color='blue'>Generate Job</Button>
          }
        </Form>
    )
}

export { JobTab }