import { Select, Tab, Label, Ref, Segment } from 'semantic-ui-react'
import { generateSelection } from '../../helpers/jsxHelper.js'
import { JobTab } from './jobTab.js'
import { useState } from 'react'
import { FilterTab } from './filterTab.js'
import { HistoryTab } from './historyTab.js'

function Sidebar(props) {
    const states = ['Arkansas', 'Virginia', 'South Carolina']
    const tabs = [
        {
            menuItem: { color: 'blue', key: 'Jobs', icon: 'object group outline', content: 'Jobs' },
            render: () => <JobTab jobLabelContent={props.jobLabelContent} addJobToHistory={ props.addJobToHistory } activeState={ props.activeState }/>,
        },
        {
            menuItem: { color: 'green', key: 'Filters', icon: 'filter', content: 'Filters' },
            render: () => <FilterTab visibility={props.visibility} setVisibility={ props.setVisibility } 
            currentMinority={props.currentMinority} setCurrentMinority={props.setCurrentMinority}
            activeState={props.activeState}/>,
        },
        {
            menuItem: { color: 'teal', key: 'History', icon: 'history', content: 'History' },
            render: () => <HistoryTab jobHistory={ props.jobHistory } removeJobFromHistory={props.removeJobFromHistory} 
            visibility={props.visibility} setVisibility={ props.setVisibility } cancelJob={ props.cancelJob } 
            setCurrentJobId={props.setCurrentJobId} defaultIndex={-1} setDistrictings={props.setDistrictings}/>,
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
          <Label horizontal basic color='violet' size='large'>Choose a State</Label>
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