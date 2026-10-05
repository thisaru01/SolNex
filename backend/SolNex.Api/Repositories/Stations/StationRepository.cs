/*
 * ------------------------------------------------------------------
 * File Name: StationRepository.cs
 * Author: Rajapaksha T.M
 * Student ID: IT23235892
 * Date: 2026-09-24
 * Description: Repository implementation for station operations.
 * ------------------------------------------------------------------
 */

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

    // Retrieves all solar stations from the MongoDB collection.
    public async Task<IEnumerable<SolarStationInfo>> GetAllStationsAsync()
    {
        return await _stations.Find(_ => true).ToListAsync();
    }

    public async Task<IEnumerable<SolarStationInfo>> SearchStationsAsync(string? search, string? status)
    {
        var builder = Builders<SolarStationInfo>.Filter;
        var filter = builder.Empty;

        if (!string.IsNullOrWhiteSpace(search))
        {
            var searchFilter = builder.Or(
                builder.Regex(s => s.StationId, new BsonRegularExpression(search, "i")),
                builder.Regex(s => s.StationName, new BsonRegularExpression(search, "i"))
            );
            filter &= searchFilter;
        }

        if (!string.IsNullOrWhiteSpace(status) && status != "All")
        {
            if (Enum.TryParse<StationStatus>(status, true, out var parsedStatus))
            {
                filter &= builder.Eq(s => s.Status, parsedStatus);
            }
        }

        return await _stations.Find(filter).ToListAsync();
    }

    // Retrieves a single solar station by its MongoDB ObjectId string.
    public async Task<SolarStationInfo?> GetStationByIdAsync(string id)
    {
        if (!ObjectId.TryParse(id, out _))
        {
            return null;
        }
        return await _stations.Find(s => s.Id == id).FirstOrDefaultAsync();
    }

    // Retrieves a single solar station by its custom business StationId (e.g. ST001).
    public async Task<SolarStationInfo?> GetStationByStationIdAsync(string stationId)
    {
        return await _stations.Find(s => s.StationId == stationId).FirstOrDefaultAsync();
    }

    // Inserts a new solar station entity into the database.
    public async Task CreateStationAsync(SolarStationInfo station)
    {
        await _stations.InsertOneAsync(station);
    }

    // Updates an existing solar station completely based on the provided entity model.
    public async Task UpdateStationAsync(string id, SolarStationInfo station)
    {
        await _stations.ReplaceOneAsync(s => s.Id == id, station);
    }

    // Deletes a solar station from the database matching the specific ObjectId.
    public async Task DeleteStationAsync(string id)
    {
        await _stations.DeleteOneAsync(s => s.Id == id);
    }

    // Calculates the distance to all active stations using the Haversine formula
    // and returns the top `limit` closest stations ordered by proximity.
    public async Task<IEnumerable<(SolarStationInfo Station, double Distance)>> GetClosestStationsAsync(double latitude, double longitude, int limit)
    {
        var stations = await _stations.Find(s => s.Status == StationStatus.Active).ToListAsync();
        
        var stationsWithDistance = stations.Select(s => 
            (Station: s, Distance: CalculateDistance(latitude, longitude, s.Latitude, s.Longitude))
        )
        .OrderBy(s => s.Distance)
        .Take(limit)
        .ToList();

        return stationsWithDistance;
    }

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
