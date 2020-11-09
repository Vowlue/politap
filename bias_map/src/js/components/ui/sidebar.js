import { Select, Tab, Label, Ref, Segment } from 'semantic-ui-react'
import { generateSelection } from '../jsxHelper.js'
import { BatchTab } from './batchTab.js'
import { useState } from 'react'
import { FilterTab } from './filterTab.js'
import { HistoryTab } from './historyTab.js'

function Sidebar(props) {
    const states = ['Arkansas', 'Virginia', 'South Carolina']
    const tabs = [
        {
            menuItem: { color: 'blue', key: 'Batches', icon: 'object group outline', content: 'Batches' },
            render: () => <BatchTab addBatchToHistory={ props.addBatchToHistory } activeState={ props.activeState }/>,
        },
        {
            menuItem: { color: 'green', key: 'Filters', icon: 'filter', content: 'Filters' },
            render: () => <FilterTab visibility={props.visibility} setVisibility={ props.setVisibility }/>,
        },
        {
            menuItem: { color: 'teal', key: 'History', icon: 'history', content: 'History' },
            render: () => <HistoryTab batchHistory={ props.batchHistory } removeBatchFromHistory={props.removeBatchFromHistory}/>,
        },
    ]

    const [ref, setRef] = useState(null)

    const activeState = () => {
        setTimeout(() => {
            let state = ref.firstChild.innerHTML
            props.setActiveState(state)
        }, 10)
    }

    return (
        <div id='sidebar'>
            <Segment basic textAlign={"center"}>
            <Label basic size='huge' color='black' icon='map' content={props.activeState ? props.activeState : 'No state selected'}></Label>
            </Segment>
          <Label horizontal basic pointing='right' color='violet' size='large'>Choose a State</Label>
            <Ref innerRef={ setRef }>
                <Select 
                    placeholder='Select a State'
                    options={ generateSelection(states) }
                    onChange={ activeState }
                />
            </Ref>
          <Tab panes={ tabs } />
        </div>
    )
}

export { Sidebar }