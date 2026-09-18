using MongoDB.Driver;
using Microsoft.Extensions.Configuration;
using SolNex.Api.Models;

namespace SolNex.Api.Data;

public class MongoDbContext
{
    private readonly IMongoDatabase _database;

    public MongoDbContext(IConfiguration configuration)
    {
        var connectionString = configuration.GetSection("DatabaseSettings:ConnectionString").Value;
        var databaseName = configuration.GetSection("DatabaseSettings:DatabaseName").Value;

        var client = new MongoClient(connectionString);
        _database = client.GetDatabase(databaseName);
    }

    public IMongoCollection<User> Users => _database.GetCollection<User>("Users");
    public IMongoCollection<SolarStationInfo> SolarStations => _database.GetCollection<SolarStationInfo>("SolarStationInfo");
    public IMongoCollection<EnergyBookingSlot> EnergyBookingSlots => _database.GetCollection<EnergyBookingSlot>("EnergyBookingSlots");
    public IMongoCollection<EnergyReservation> EnergyReservations => _database.GetCollection<EnergyReservation>("EnergyReservations");
    public IMongoCollection<EnergyTransaction> EnergyTransactions => _database.GetCollection<EnergyTransaction>("EnergyTransactions");
    
    // We expose this for our /test-db endpoint
    public IMongoDatabase Database => _database;
}