package adf.agent.info;

import rescuecore2.misc.Pair;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.ChangeSet;
import rescuecore2.worldmodel.Entity;
import rescuecore2.worldmodel.EntityID;
import rescuecore2.worldmodel.WorldModel;

import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.stream.Collectors;

import static rescuecore2.standard.entities.StandardEntityURN.*;

public class WorldInfo implements Iterable<StandardEntity> {
	private StandardWorldModel world;
	private ChangeSet changed;
    private int time;

    public WorldInfo(StandardWorldModel world) {
		this.setWorld(world);
        this.time = -1;
		this.updateTimeOfBuriedMap = -1;
		this.buriedMap = new HashMap<>();
	}

	public void setWorld(StandardWorldModel world)
	{
		this.world = world;
	}

	public StandardWorldModel getRawWorld() {
		return this.world;
	}

	public void setChanged(ChangeSet changed) {
		this.changed = changed;
	}

	public ChangeSet getChanged() {
		return this.changed;
	}

	public Collection<EntityID> getObjectIDsInRange(EntityID entity, int range) {
		return this.convertToID(this.world.getObjectsInRange(entity, range));
	}

	public Collection<EntityID> getObjectIDsInRange(StandardEntity entity, int range) {
		return this.convertToID(this.world.getObjectsInRange(entity, range));
	}

	public Collection<EntityID> getObjectIDsInRange(int x, int y, int range) {
		return this.convertToID(this.world.getObjectsInRange(x,y,range));
	}

	public Collection<EntityID> getObjectIDsInRectangle(int x1, int y1, int x2, int y2) {
		return this.convertToID(this.world.getObjectsInRectangle(x1, y1, x2, y2));
	}

	public Collection<EntityID> getEntityIDsOfType(StandardEntityURN urn) {
		return this.convertToID(this.world.getEntitiesOfType(urn));
	}

	public Collection<EntityID> getEntityIDsOfType(StandardEntityURN... urns) {
		return this.convertToID(this.world.getEntitiesOfType(urns));
	}

    public StandardEntity getPosition(Human entity) {
        return entity.getPosition(this.world);
    }

    public StandardEntity getPosition(EntityID entityID) {
        StandardEntity entity = this.getEntity(entityID);
        return (entity instanceof Human) ? this.getPosition((Human)entity) : null;
    }

    public Pair<Integer, Integer> getLocation(StandardEntity entity) {
        return entity.getLocation(this.world);
    }

    public Pair<Integer, Integer> getLocation(EntityID entityID) {
        return this.getLocation(this.getEntity(entityID));
    }

    //org

	public void merge(ChangeSet changeSet) {
		this.world.merge(changeSet);
	}

	public void indexClass(StandardEntityURN... urns) {
		this.world.indexClass(urns);
	}

	public void index() {
		this.world.index();
	}

	public Collection<StandardEntity> getObjectsInRange(EntityID entity, int range) {
		return this.world.getObjectsInRange(entity, range);
	}

	public Collection<StandardEntity> getObjectsInRange(StandardEntity entity, int range) {
		return this.world.getObjectsInRange(entity, range);
	}

	public Collection<StandardEntity> getObjectsInRange(int x, int y, int range) {
		return this.world.getObjectsInRange(x, y, range);
	}

	public Collection<StandardEntity> getObjectsInRectangle(int x1, int y1, int x2, int y2) {
		return this.world.getObjectsInRectangle(x1, y1, x2, y2);
	}

	public Collection<StandardEntity> getEntitiesOfType(StandardEntityURN urn) {
		return this.world.getEntitiesOfType(urn);
	}

	public Collection<StandardEntity> getEntitiesOfType(StandardEntityURN... urns) {
		return this.world.getEntitiesOfType(urns);
	}

	public int getDistance(EntityID first, EntityID second) {
		return this.world.getDistance(first, second);
	}

	public int getDistance(StandardEntity first, StandardEntity second) {
		return this.world.getDistance(first, second);
	}

	public Rectangle2D getBounds() {
		return this.world.getBounds();
	}

	public Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> getWorldBounds() {
		return this.world.getWorldBounds();
	}

	public static StandardWorldModel createStandardWorldModel(WorldModel<? extends Entity> existing) {
		return StandardWorldModel.createStandardWorldModel(existing);
	}

    public StandardEntity getEntity(EntityID id) {
        return this.world.getEntity(id);
    }

    public Collection<StandardEntity> getAllEntities() {
        return this.world.getAllEntities();
    }

    public void addEntity(Entity e) {
        this.world.addEntity(e);
    }

    public void addEntities(Collection<? extends Entity> e) {
        this.world.addEntities(e);
    }

    public void removeEntity(StandardEntity e) {
        this.world.removeEntity(e.getID());
    }

    public void removeEntity(EntityID id) {
        this.world.removeEntity(id);
    }

    public void removeAllEntities() {
        this.world.removeAllEntities();
    }

	@Override
    public Iterator<StandardEntity> iterator() {
        return this.world.iterator();
    }

	private Collection<EntityID> convertToID(Collection<StandardEntity> entities) {
		return entities.stream().map(StandardEntity::getID).collect(Collectors.toList());
	}

	private Set<Building> getFireBuildingSet() {
	    Set<Building> fireBuildings = new HashSet<>();
        for(StandardEntity entity : this.getEntitiesOfType(BUILDING, GAS_STATION, AMBULANCE_CENTRE, FIRE_STATION, POLICE_OFFICE)) {
            Building building = (Building)entity;
            if(building.isOnFire()) fireBuildings.add(building);
        }
        return fireBuildings;
    }

    public List<Building> getFireBuildings() {
        return new ArrayList<>(this.getFireBuildingSet());
    }

    public List<EntityID> getFireBuildingIDs() {
        return this.getFireBuildingSet().stream().map(Building::getID).collect(Collectors.toList());
    }

	public int getNumberOfBuried(EntityID entityID) {
		int value = 0;
		for(StandardEntity entity : this.getEntitiesOfType(CIVILIAN, AMBULANCE_TEAM, FIRE_BRIGADE, POLICE_FORCE)) {
			Human human = (Human)entity;
			if(this.getPosition(human).getID().getValue() == entityID.getValue()) {
				if(human.isBuriednessDefined() && human.getBuriedness() > 0) {
					value++;
				}
			}
		}
		return value;
	}

	private Map<Integer, Map<EntityID, Set<EntityID>>> buriedMap;
	private int updateTimeOfBuriedMap;

	public int getNumberOfBuried(int time, EntityID entityID) {
		this.updateBuriedMap();
		Map<EntityID, Set<EntityID>> targetTimeBuriedMap =  this.buriedMap.get(time > 0 ? time : this.time + time);
		if(targetTimeBuriedMap == null) return -1;
		Set<EntityID> buriedList = targetTimeBuriedMap.get(entityID);
		if(buriedList == null) return -1;
		return buriedList.size();
	}

    private void updateBuriedMap() {
    	if(this.updateTimeOfBuriedMap != this.time) {
			Map<EntityID, Set<EntityID>> currentMap = this.buriedMap.get(this.time);
			if(currentMap == null) currentMap = new HashMap<>();
			for(StandardEntity entity : this.getEntitiesOfType(CIVILIAN, AMBULANCE_TEAM, FIRE_BRIGADE, POLICE_FORCE)) {
				Human human = (Human)entity;
				if(human.isBuriednessDefined() && human.getBuriedness() > 0) {
					EntityID position = human.getPosition();
					Set<EntityID> buriedSet = currentMap.get(position);
					if(buriedSet == null) buriedSet = new HashSet<>();
					buriedSet.add(human.getID());
					currentMap.put(position, buriedSet);
				}
			}
			this.buriedMap.put(this.time, currentMap);
		}
	}

    public void setTime(int time) {
        this.time = time;
    }

	private Map<EntityID, StandardEntity> cache;
	private Map<Integer, Map<EntityID, StandardEntity>> rollbackData;

	private void createCache() {
		this.cache.clear();
		for(StandardEntity entity : this) {
			StandardEntityURN urn = entity.getStandardURN();
			if(urn == ROAD) this.cache.put(entity.getID(), new Road((Road)entity));
			else if(urn == BUILDING) this.cache.put(entity.getID(), new Building((Building) entity));
			else if(urn == CIVILIAN) this.cache.put(entity.getID(), new Civilian((Civilian) entity));
			else if(urn == BLOCKADE) this.cache.put(entity.getID(), new Blockade((Blockade)entity));
			else if(urn == FIRE_BRIGADE) this.cache.put(entity.getID(), new FireBrigade((FireBrigade) entity));
			else if(urn == AMBULANCE_TEAM) this.cache.put(entity.getID(), new AmbulanceTeam((AmbulanceTeam)entity));
			else if(urn == POLICE_FORCE) this.cache.put(entity.getID(), new PoliceForce((PoliceForce) entity));
			else if(urn == HYDRANT) this.cache.put(entity.getID(), new Hydrant((Hydrant) entity));
			else if(urn == REFUGE) this.cache.put(entity.getID(), new Refuge((Refuge) entity));
			else if(urn == GAS_STATION) this.cache.put(entity.getID(), new GasStation((GasStation)entity));
			else if(urn == FIRE_STATION) this.cache.put(entity.getID(), new FireStation((FireStation) entity));
			else if(urn == AMBULANCE_CENTRE) this.cache.put(entity.getID(), new AmbulanceCentre((AmbulanceCentre) entity));
			else if(urn == POLICE_OFFICE) this.cache.put(entity.getID(), new PoliceOffice((PoliceOffice) entity));
			else if(urn == WORLD) this.cache.put(entity.getID(), new World((World) entity));
		}
	}

	public void createRollbackData() {
		Map<EntityID, StandardEntity> data = new HashMap<>();
		for(EntityID entityID : this.changed.getChangedEntities()) {
			data.put(entityID, this.cache.get(entityID));
		}
		this.rollbackData.put(this.time - 1, data);
	}
}
