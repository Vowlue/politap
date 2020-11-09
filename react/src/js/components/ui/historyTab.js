import { Label, Header, Icon, Form, Divider, Message, Segment } from 'semantic-ui-react'

function HistoryTab(props) {
  const renderHistory = jobInfo => {
    return (
      <Segment key={jobInfo.id}>
        <Label onClick={() => props.removeJobFromHistory(jobInfo.id)} color='red' icon='x' corner='right'></Label>
        <Header textAlign='center' as='h4'>
          {`Job #${jobInfo.id}`}
          <Header.Subheader>
              <Label size='large'>
                <Icon name='star'></Icon>
                State
                <Label.Detail content={jobInfo.state}> 
                </Label.Detail>
              </Label>
              <Label size='large'>
                <Icon name='plus'></Icon>
                Status
                <Label.Detail content={jobInfo.status}> 
                </Label.Detail>
              </Label>
          </Header.Subheader>
        </Header>
        <Label  size='large' color='teal' basic >
          Plans:
            <Label.Detail content={jobInfo.plans}>
            </Label.Detail>
        </Label>
        <Label  size='large' color='teal' basic >
            Population Variance:
            <Label.Detail content={jobInfo.populationVariance}>
            </Label.Detail>
        </Label>
        <Label  size='large' color='teal' basic >
            Compactness:
            <Label.Detail content={jobInfo.compactness}>
            </Label.Detail>
        </Label>
        <Label  size='large' color='teal' basic >
            Racial/Ethnic Groups:
            <Label.Detail content={jobInfo.groups.length > 0 ? jobInfo.groups.join(", ") : "None"}>
            </Label.Detail>
        </Label>
        <Label  size='large' color='teal' basic >
            Server:
            <Label.Detail content={jobInfo.server}>
            </Label.Detail>
        </Label>
      </Segment>
    )
  }

  return (
      <Form>
        <Message>
          <Header textAlign='center' as='h2'>
            Job History
            <Header.Subheader>
            View the history of generated jobs.
            </Header.Subheader>
          </Header>
        </Message>
        <Divider horizontal>
          <Header as='h4'>
            <Icon name='bars' />
            Jobs
          </Header>
        </Divider>
        <Segment style={{ overflow: 'auto', maxHeight: '64vh' }}>
          { 
            Object.keys(props.jobHistory).map(key => 
              renderHistory(props.jobHistory[key]))
          }
        </Segment>
      </Form>
  )
}

export { HistoryTab }