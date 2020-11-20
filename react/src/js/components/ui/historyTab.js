import { Label, Header, Icon, Form, Divider, Message, Segment, Button, Grid, Popup, Accordion } from 'semantic-ui-react'
import Plot from 'react-plotly.js'

function HistoryTab(props) {
  const renderHistory = jobInfo => {
    const panels = [
      {
        key: 'more-options',
        title: 'More Options',
        content: {
          content: (<div>
            <Popup trigger={<Button color='teal' content='Box Plot' />}>
              <Popup.Content>
                <Plot
                    data={[
                      {
                        x: [1, 2, 3,4,5,6,7,8,9],
                        type: 'box',
                        marker: {color: 'red'},
                      }
                    ]}
                    layout={ {width: 320, height: 240, title: 'A Fancy Plot'} }
                  />
              </Popup.Content>
            </Popup>
            <div>Districts</div>
            <Button 
              color='red'
              basic={!props.visibility.random} 
              onClick={() => {
                props.setCurrentJobId(jobInfo.id)
                props.setVisibility('random', !props.visibility.random)
              }}
              content="Random"
            />
            <Button 
              color='green'
              basic={!props.visibility.random2} 
              onClick={() => {
                props.setCurrentJobId(jobInfo.id)
                props.setVisibility('random2', !props.visibility.random2)
              }}
              content="Random"
            />
            <Button>Average</Button>
            <Button>Extreme</Button>
            </div>)}
      }
    ]
    return (
      <Segment key={jobInfo.id}>
        <Label onClick={() => props.removeJobFromHistory(jobInfo.id)} color='red' icon='x' corner='right'></Label>
        <Header textAlign='center' as='h4'>
          {`Job #${jobInfo.id}`}
          <Header.Subheader>
              <Label color='black' size='large'>
                <Icon name='star'></Icon>
                State
                <Label.Detail content={jobInfo.state}> 
                </Label.Detail>
              </Label>
              <Label color='green' size='large'>
                Status
                <Label.Detail content={jobInfo.status}> 
                </Label.Detail>
              </Label>
          </Header.Subheader>
        </Header>
        <Grid columns='equal' divided padded>
          <Grid.Row>
            <Grid.Column>
              <Label size='large' color='teal' basic >
              Plans:
                <Label.Detail content={jobInfo.plans}>
                </Label.Detail>
              </Label>
              <Label size='large' color='teal' basic >
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
              Server:
                <Label.Detail content={jobInfo.server}>
                </Label.Detail>
              </Label>
            </Grid.Column>
            <Grid.Column stretched>
              <Label  size='large' color='teal' basic >
              Racial/Ethnic Groups:
                <Label.Detail content={jobInfo.groups.length > 0 ? jobInfo.groups.join(", ") : "None"}>
                </Label.Detail>
              </Label>
            </Grid.Column>
          </Grid.Row>
        </Grid>
        <br />
        {
          jobInfo.status !== "Running" ?
          <Button color='red' basic >Cancel Job</Button>
          :
          <Button color='red' onClick={() => props.cancelJob(jobInfo.id)}>Cancel Job</Button>
        }
        {
          jobInfo.status === "Done" ? 
          <Accordion defaultActiveIndex={-1} panels={panels} />
          :
          jobInfo.status === "Canceled" ? null
          :
          <div>Loading options...</div>
        }
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