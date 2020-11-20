import json

precincts = {}

state = input('Please enter a State: ')

def parseTable(file,pop_vap):
    #Column of race code in the csv file {"column in csv":race code}
    race_index = {}
    tablerow = file.readline().strip().strip('"').split('","')
    for code in range(2,len(tablerow)):
        if tablerow[code] in race_code:
            race_index[code] = tablerow[code]

    file.readline()
    for line in file:
        demographics = {"Total":0,
                        "Hispanic or Latino":0,
                        "White":0,
                        "Black or African American":0,
                        "American Indian and Alaska Native":0,
                        "Asian":0,"Native Hawaiian and Other Pacific Islander":0,
                        "Some Other Race":0}
        tablerow = line.strip().strip('"').split('","')
        for race_list in race_index:
            for race in race_code[race_index[race_list]]:
                demographics[race] = demographics[race] + int(tablerow[race_list])
        
        precinct_county = tablerow[1].split(", ")
        if (pop_vap == 'pop'):
            precincts[tablerow[0].split("US")[1]] = {"demographics":demographics,"county":precinct_county[1]}    
        else:
            precincts[tablerow[0].split("US")[1]]['vap'] = demographics

#Dictionary for Census Code to Population combination {race code: [races]}
race_code = {}

def parseMetaData(file):
    for line in file:
        code_demographic = line.split(",")
        race = code_demographic[1].split('!!')[-1].strip()
        code = code_demographic[0]
        if " alone" in race:
            race_code[code] = [race.replace(' alone','')]
        else:
            race_code[code] = race.split("; ")

with open('censusPOPMetaData.csv') as censusCode:
    parseMetaData(censusCode)

with open('censusVAPMetaData.csv') as censusCode:
    parseMetaData(censusCode)

with open(state+'/'+state+'_POP.csv') as censusPopulation:
    parseTable(censusPopulation,'pop')

with open(state+'/'+state+'_VAP.csv') as censusPopulation:
    parseTable(censusPopulation,'vap')

with open(state+'/'+state+'_map.json') as precinctmap:
    geomap = json.load(precinctmap)
    
    for precinct in geomap["features"]:
        precinctproperty = precinct["properties"]
        geoid = precinctproperty['STATEFP10'] + precinctproperty['COUNTYFP10'] + '0'*(6-len(precinctproperty['VTDST10']))+precinctproperty['VTDST10']
        precinctinfo = precincts[geoid]  
        newproperties = {}
        newproperties['GEOID'] = geoid
        newproperties['COUNTY'] = precinctinfo['county']
        newproperties['NAME'] = precinctproperty['NAMELSAD10']
        for demo in precinctinfo['demographics']:
            newproperties[demo] = precinctinfo['demographics'][demo]
        for vap in precinctinfo['vap']:
            newproperties[vap+' VAP'] = precinctinfo['vap'][vap]
        precinct['properties'] = newproperties
    with open(state+"_Precinct.json",'w') as outfile:
        json.dump(geomap,outfile)
        
print('finished demographics')