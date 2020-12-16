import { Button, Input, Header, Icon, Dropdown, Form, Label, Divider, Message, Ref} from 'semantic-ui-react'
import { generateSelection, createCheckbox } from '../../helpers/jsxHelper.js'
import { useState } from 'react'

const censusCategories = ['White', 'Black or African American', 'American Indian or Alaska Native', 'Asian', 'Native Hawaiian or Other Pacific Islander', 'Other']
const compactness = ['Not', 'Somewhat', 'Very', 'Extremely']

function JobTab(props) {
    const jobInfo = {
        plans: 10,
        populationVariance: 0.05,
        compactness: 'Somewhat',
        groups: [],
    }

    const [compactRef, setCompactRef] = useState(null) 
    const setPlans = (e) => {jobInfo.plans = e.target.value}
    const setPopulationVariance = (e) => {jobInfo.populationVariance = e.target.value}
    const setCompactness = () => {setTimeout(() => {
      let c = compactRef.firstChild.innerHTML
      jobInfo.compactness = c}, 10)}
    const setGroup = (label) => {setTimeout(() => {
      const index = jobInfo.groups.indexOf(label);
      if (index > -1) {
        jobInfo.groups.splice(index, 1);
      }
      else {
        jobInfo.groups.push(label)
      }}, 10)}
    const sendJob = () => {
      jobInfo.status = "Initialized"
      jobInfo.server = jobInfo.plans <= 15 ? 'Local' : 'Seawulf'
      props.addJobToHistory(jobInfo)
    }

    return (
        <Form>
          <Message>
            <Header textAlign='center' as='h2'>
              Job Configuration
              <Header.Subheader>
              Configure a job to run.
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
          <Form.Field>
            <Label color='blue' size='large'>Racial / Ethnic Groups</Label>
          </Form.Field>
          {censusCategories.map(category => createCheckbox(category, setGroup))}
          <Form.Field>
            Generated graphs should be{' '}
            <Ref innerRef={setCompactRef}>
              <Dropdown floating inline options={generateSelection(compactness)} onChange={setCompactness} defaultValue={jobInfo.compactness} />
            </Ref>
            {' '}compact.
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