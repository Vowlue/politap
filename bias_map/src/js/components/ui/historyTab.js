import { Label, Header, Icon, Form, Divider, Message, Segment } from 'semantic-ui-react'

function HistoryTab(props) {
  const renderHistory = batchInfo => {
    return (
      <Segment key={batchInfo.id}>
        <Label onClick={() => props.removeBatchFromHistory(batchInfo.id)} color='red' icon='x' corner='right'></Label>
        <Header textAlign='center' as='h4'>
          {`Batch #${batchInfo.id}`}
          <Header.Subheader>
              <Label size='large'>
                <Icon name='star'></Icon>
                State
                <Label.Detail content={batchInfo.state}> 
                </Label.Detail>
              </Label>
          </Header.Subheader>
        </Header>
        <Label  size='large' color='teal' basic >
          Plans:
            <Label.Detail content={batchInfo.plans}>
            </Label.Detail>
        </Label>
        <Label  size='large' color='teal' basic >
            Population Variance:
            <Label.Detail content={batchInfo.populationVariance}>
            </Label.Detail>
        </Label>
        <Label  size='large' color='teal' basic >
            Compactness:
            <Label.Detail content={batchInfo.compactness}>
            </Label.Detail>
        </Label>
        <Label  size='large' color='teal' basic >
            Racial/Ethnic Groups:
            <Label.Detail content={batchInfo.groups.length > 0 ? batchInfo.groups.join(", ") : "None"}>
            </Label.Detail>
        </Label>
        <Label  size='large' color='teal' basic >
            Server:
            <Label.Detail content={batchInfo.server}>
            </Label.Detail>
        </Label>
      </Segment>
    )
  }

  return (
      <Form>
        <Message>
          <Header textAlign='center' as='h2'>
            Batch History
            <Header.Subheader>
            View the history of generated batches.
            </Header.Subheader>
          </Header>
        </Message>
        <Divider horizontal>
          <Header as='h4'>
            <Icon name='bars' />
            Batches
          </Header>
        </Divider>
        <Segment style={{ overflow: 'auto', maxHeight: '64vh' }}>
          { 
            Object.keys(props.batchHistory).map(key => 
              renderHistory(props.batchHistory[key]))
          }
        </Segment>
      </Form>
  )
}

export { HistoryTab }