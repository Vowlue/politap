import { Header, Icon, Form, Divider, Message, List, Button } from 'semantic-ui-react'

function FilterTab(props) {
    return (
        <Form>
          <Message>
            <Header textAlign='center' as='h2'>
              Map View Filter
              <Header.Subheader>
              Toggle map views filters on/off.
              </Header.Subheader>
            </Header>
          </Message>
          <Divider horizontal>
            <Header as='h4'>
              <Icon name='sort' />
              Default Filters
            </Header>
          </Divider>
          {props.activeState ?
          <List selection>
            <List.Item>
            <Button
              color='blue'
              basic={!props.visibility.district} 
              content='Initial Districts' 
              size='medium'
              onClick={() => {
                props.setVisibility('district', !props.visibility.district)
              }}
            />
            </List.Item>
            <List.Item>
            <Button
              color='black' 
              basic={!props.visibility.precinct} 
              content='Precincts' 
              size='medium'
              onClick={() => {
                props.setVisibility('precinct', !props.visibility.precinct)
              }}
            />
            </List.Item>
          </List>
          :
          null}
          <Divider horizontal>
            <Header as='h4'>
              <Icon name='sort' />
              Heat Map Filters
            </Header>
          </Divider>
          {props.activeState ?
          <List selection>
            <List.Item>
              <Button
                color='purple' 
                basic={props.currentMinority !== 'Black or African American'} 
                content='Black/African American' 
                size='medium'
                onClick={() => {
                  props.setCurrentMinority('Black or African American')
                }}
              />
            </List.Item>
            <List.Item>
              <Button
                color='red' 
                basic={props.currentMinority !== 'Hispanic or Latino'} 
                content='Hispanic/Latino' 
                size='medium'
                onClick={() => {
                  props.setCurrentMinority('Hispanic or Latino')
                }}
              />
            </List.Item>
            <List.Item>
              <Button
                color='orange' 
                basic={props.currentMinority !== 'White'} 
                content='White' 
                size='medium'
                onClick={() => {
                  props.setCurrentMinority('White')
                }}
              />
            </List.Item>
            <List.Item>
              <Button
                color='green' 
                basic={props.currentMinority !== 'Asian'} 
                content='Asian' 
                size='medium'
                onClick={() => {
                  props.setCurrentMinority('Asian')
                }}
              />
            </List.Item>
            <List.Item>
              <Button
                color='teal' 
                basic={props.currentMinority !== 'American Indian and Alaska Native'} 
                content='American Indian/Alaskan' 
                size='medium'
                onClick={() => {
                  props.setCurrentMinority('American Indian and Alaska Native')
                }}
              />
            </List.Item>
            <List.Item>
              <Button
                color='pink' 
                basic={props.currentMinority !== 'Native Hawaiian and Other Pacific Islander'} 
                content='Hawaiian/Pacific Islander' 
                size='medium'
                onClick={() => {
                  props.setCurrentMinority('Native Hawaiian and Other Pacific Islander')
                }}
              />
            </List.Item>
            <List.Item>
              <Button
                color='brown' 
                basic={props.currentMinority !== 'Some Other Race'} 
                content='Other Race' 
                size='medium'
                onClick={() => {
                  props.setCurrentMinority('Some Other Race')
                }}
              />
            </List.Item>
          </List>
          :
          null}
        </Form>
    )
}

export { FilterTab }