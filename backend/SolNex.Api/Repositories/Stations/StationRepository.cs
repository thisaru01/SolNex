using MongoDB.Driver;
using SolNex.Api.Data;
using SolNex.Api.Models;
using MongoDB.Bson;

namespace SolNex.Api.Repositories.Stations;

public class StationRepository : IStationRepository
{
    private readonly IMongoCollection<SolarStationInfo> _stations;

    public StationRepository(MongoDbContext dbContext)
    {
        _stations = dbContext.SolarStations;
    }

    public async Task<IEnumerable<SolarStationInfo>> GetAllStationsAsync()
    {
        return await _stations.Find(_ => true).ToListAsync();
    }

    public async Task<SolarStationInfo?> GetStationByIdAsync(string id)
    {
        if (!ObjectId.TryParse(id, out _))
        {
            return null;
        }
        return await _stations.Find(s => s.Id == id).FirstOrDefaultAsync();
    }

    public async Task<SolarStationInfo?> GetStationByStationIdAsync(string stationId)
    {
        return await _stations.Find(s => s.StationId == stationId).FirstOrDefaultAsync();
    }

    public async Task CreateStationAsync(SolarStationInfo station)
    {
        await _stations.InsertOneAsync(station);
    }

    public async Task UpdateStationAsync(string id, SolarStationInfo station)
    {
        await _stations.ReplaceOneAsync(s => s.Id == id, station);
    }

    public async Task<IEnumerable<SolarStationInfo>> GetNearbyStationsAsync(double latitude, double longitude, double radiusInKm)
    {
        // For a basic implementation without geospatial indexes, we fetch all and calculate distance
        // A better approach would be to use 2dsphere indexes and $near queries, 
        // but this depends on how the DB is configured. We use a simple in-memory calculation here.
        var stations = await _stations.Find(s => s.Status == StationStatus.Active).ToListAsync();
        
        var nearbyStations = stations.Where(s => 
            CalculateDistance(latitude, longitude, s.Latitude, s.Longitude) <= radiusInKm
        ).ToList();

        return nearbyStations;
    }

    // Haversine formula for calculating distance between two coordinates in kilometers
    private double CalculateDistance(double lat1, double lon1, double lat2, double lon2)
    {
        var R = 6371; // Earth's radius in km
        var dLat = ToRadians(lat2 - lat1);
        var dLon = ToRadians(lon2 - lon1);
        var a = Math.Sin(dLat / 2) * Math.Sin(dLat / 2) +
                Math.Cos(ToRadians(lat1)) * Math.Cos(ToRadians(lat2)) *
                Math.Sin(dLon / 2) * Math.Sin(dLon / 2);
        var c = 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1 - a));
        return R * c;
    }

    private double ToRadians(double angle)
    {
        return Math.PI * angle / 180.0;
    }
}
