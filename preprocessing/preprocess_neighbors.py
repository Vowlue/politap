import math
import json
from shapely.geometry import shape
from shapely.strtree import STRtree

radius = 6371 #Earth Radius in KM
km_to_feet = 3280.84

state = input('Please enter a State: ')

neighbors = {}
shape_geoid = {}
shapeList = []
geoid_population = {}

def coordToCartesian(lon,lat):
    lonRad = math.radians(lon)
    latRad = math.radians(lat)
    xCoor = radius * km_to_feet * math.cos(latRad) * math.cos(lonRad)
    yCoor = radius * km_to_feet * math.cos(latRad) * math.sin(lonRad)
    return [xCoor,yCoor]

with open('ExplodedMap/'+state+'_PrecinctE.json') as precinctMap:
    precincts = json.load(precinctMap)
    for precinct in precincts['features']:
        cartShape = []
        for shapes in precinct['geometry']['coordinates']:
            linring = []
            for coords in shapes:
                linring.append(coordToCartesian(coords[0],coords[1]))
            cartShape.append(linring)
        XYShape = shape({'type':'Polygon','coordinates':cartShape})
        shapeList.append(XYShape)
        shape_geoid[id(XYShape)] = precinct['properties']['GEOID']
        geoid_population[precinct['properties']['GEOID']] = precinct['properties']['Total']

tree = STRtree(shapeList)
for precinctShape in shapeList:
    queryShape = precinctShape.buffer(200)
    neighborList = []
    for neighbor in tree.query(queryShape):
        if shape_geoid[id(neighbor)] == shape_geoid[id(precinctShape)]:
            continue
        if shape_geoid[id(neighbor)] in neighborList:
            continue
        border = queryShape.intersection(neighbor).length
        if border > 200:
            neighborList.append(shape_geoid[id(neighbor)])
    neighbors[shape_geoid[id(precinctShape)]] = {'neighbor':neighborList,'population':geoid_population[shape_geoid[id(precinctShape)]]}

with open(state+"_Neighbor.json",'w') as outfile:
    json.dump(neighbors,outfile)

print('finished neighbors')