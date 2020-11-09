import { Header, Icon, Form, Divider, Message, List, Button } from 'semantic-ui-react'

function FilterTab(props) {

    return (
        <Form>
          <Message>
            <Header textAlign='center' as='h2'>
              Map View Filter
              <Header.Subheader>
              Toggle map views on and off.
              </Header.Subheader>
            </Header>
          </Message>
          <Divider horizontal>
            <Header as='h4'>
              <Icon name='sort' />
              Filters
            </Header>
          </Divider>
          <List divided selection>
            <List.Item>
            <Button
              color='green' 
              basic={!props.visibility.district} 
              content='Initial Districts' 
              size='large'
              onClick={() => {
                props.setVisibility('district', !props.visibility.district)
              }}
            />
            </List.Item>
            <List.Item>
            <Button
              color='green' 
              basic={!props.visibility.precinct} 
              content='Precincts' 
              size='large'
              onClick={() => {
                props.setVisibility('precinct', !props.visibility.precinct)
              }}
            />
            </List.Item>
          </List>
        </Form>
    )
}

export { FilterTab }