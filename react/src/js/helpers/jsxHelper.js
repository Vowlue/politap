import { Checkbox, Form } from "semantic-ui-react"

const generateSelection = choices => {
    const ret = []
    choices.forEach(choice => ret.push({key: choice, text: choice, value: choice}))
    return ret
}
const createCheckbox = (label, setFunc) => {
    return (
        <Form.Field key={label}>
            <Checkbox
                onChange={() => setFunc(label)}
                toggle
                label={label}
                name={label}
                value={label}
            />
        </Form.Field>
    )
}

export { generateSelection, createCheckbox }