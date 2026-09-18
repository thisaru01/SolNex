using MongoDB.Driver;
using Microsoft.Extensions.Configuration;

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

    // You will add your collections here later, for example:
    // public IMongoCollection<User> Users => _database.GetCollection<User>("Users");
    
    // We expose this for our /test-db endpoint
    public IMongoDatabase Database => _database;
}