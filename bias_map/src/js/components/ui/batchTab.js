import { Button, Select, Input, Header, Icon, Dropdown, Form, Label, Divider, Message, Ref} from 'semantic-ui-react'
import { generateSelection, createCheckbox } from '../jsxHelper.js'
import { useState } from 'react'

const servers = ['Local', 'Seawulf']
const censusCategories = ['White', 'Black or African American', 'American Indian or Alaska Native', 'Asian', 'Native Hawaiian or Other Pacific Islander', 'Other']
const compactness = ['Not', 'Somewhat', 'Very', 'Extremely']

function BatchTab(props) {
    const batchInfo = {
        plans: 5000,
        populationVariance: 1000,
        compactness: 'Somewhat',
        server: 'Local',
        groups: [],
    }

    const [compactRef, setCompactRef] = useState(null) 
    const [serverRef, setServerRef] = useState(null)

    const setPlans = (e) => {batchInfo.plans = e.target.value}
    const setPopulationVariance = (e) => {batchInfo.populationVariance = e.target.value}
    const setCompactness = () => {setTimeout(() => {
      let c = compactRef.firstChild.innerHTML
      batchInfo.compactness = c}, 10)}
    const setServer = () => {setTimeout(() => {
      let s = serverRef.firstChild.innerHTML
      batchInfo.server = s}, 10)}
    const setGroup = (label) => {setTimeout(() => {
      const index = batchInfo.groups.indexOf(label);
      if (index > -1) {
        batchInfo.groups.splice(index, 1);
      }
      else {
        batchInfo.groups.push(label)
      }}, 10)}
    const sendBatchJob = () => {
      props.addBatchToHistory({...batchInfo, id: parseInt(Math.random()*1000)})
    }

    return (
        <Form>
          <Message>
            <Header textAlign='center' as='h2'>
              Batch Configuration
              <Header.Subheader>
              Configure your own batch job to run.
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
            <Input type='number' labelPosition='left' placeholder={batchInfo.plans}>
              <Label color='blue'># of Plans</Label>
              <input min='1' onChange={setPlans.bind(this)}/>
            </Input>
          </Form.Field>
          <Form.Field>
            <Input type='number' labelPosition='left' placeholder={batchInfo.populationVariance}>
              <Label color='blue'>Population Variance</Label>
              <input min='1' onChange={setPopulationVariance.bind(this)}/>
            </Input>
          </Form.Field>
          <Form.Field inline>
            <Label color='blue' pointing='right' size='large'>Compactness</Label>
            <Ref innerRef={setCompactRef}>
              <Select onChange={setCompactness} placeholder={batchInfo.compactness} options={generateSelection(compactness)} />
            </Ref>
          </Form.Field>
          <Form.Field>
            <Label color='blue' size='large'>Racial / Ethnic Groups</Label>
          </Form.Field>
          {censusCategories.map(category => createCheckbox(category, setGroup))}
          <Form.Field>
            Run this batch job in the{' '}
            <Ref innerRef={setServerRef}>
              <Dropdown floating inline options={generateSelection(servers)} onChange={setServer} defaultValue={batchInfo.server} />
            </Ref>
            {' '}server.
          </Form.Field>
          {
            props.activeState ? <Button onClick={sendBatchJob} color='blue'>Generate Batch</Button>
            : <Button disabled basic color='blue'>Generate Batch</Button>
          }
        </Form>
    )
}

export { BatchTab }