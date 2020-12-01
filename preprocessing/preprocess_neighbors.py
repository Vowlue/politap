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
geoid_area = {}

def coordToCartesian(lon,lat):
    lonRad = math.radians(lon)
    latRad = math.radians(lat)
    xCoor = radius * km_to_feet * lonRad * math.cos(latRad)
    yCoor = radius * km_to_feet * latRad
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
        geoid = precinct['properties']['GEOID']
        shape_geoid[id(XYShape)] = geoid
        geoid_population[geoid] = precinct['properties']['Total']
        if geoid in geoid_area:
            geoid_area[geoid] = geoid_area[geoid]+XYShape.area
        else:
            geoid_area[geoid] = XYShape.area
tree = STRtree(shapeList)
for precinctShape in shapeList:
    queryShape = precinctShape.buffer(200)
    geoid = shape_geoid[id(precinctShape)]
    if geoid in neighbors:
        neighborList = neighbors[geoid]['neighbor']
    else:
        neighborList = {}
    for neighbor in tree.query(queryShape):
        neighborId = shape_geoid[id(neighbor)]
        if neighborId == geoid:
            continue
        border = queryShape.intersection(neighbor).length
        if neighborId in neighborList:
            neighborList[neighborId] = neighborList[neighborId]+border
            continue
        if border > 200:
            neighborList[neighborId] = border
    neighbors[geoid] = {'neighbor':neighborList,'population':geoid_population[geoid],'area':geoid_area[geoid]}

with open(state+'/'+state+"_Neighbor.json",'w') as outfile:
    json.dump(neighbors,outfile)

print('finished neighbors')