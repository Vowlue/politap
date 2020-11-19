const convertEnumToString = en => en.substring(0,1) + en.substring(1,en.length).toLowerCase()

const stringifyNumber = num => {
    const number = num.toString().split("").reverse()
    let arrayNumber = []
    let counter = 0
    for(const digit of number) {
        if(counter === 3) {
            arrayNumber.push(",")
            counter = 0
        }
        arrayNumber.push(digit)
        counter++
    }
    return arrayNumber.reverse().join("")
}

export { convertEnumToString, stringifyNumber }