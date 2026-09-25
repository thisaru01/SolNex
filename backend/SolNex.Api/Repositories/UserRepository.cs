using MongoDB.Driver;
using SolNex.Api.Data;
using SolNex.Api.Models;

namespace SolNex.Api.Repositories;

public sealed partial class UserRepository : IUserRepository
{
    private readonly IMongoCollection<User> _users;

    public UserRepository(MongoDbContext dbContext)
    {
        _users = dbContext.Users;
    }

    public Task<bool> ExistsByNicOrEmailAsync(
        string nic,
        string email,
        CancellationToken cancellationToken = default)
    {
        var filter = Builders<User>.Filter.Or(
            Builders<User>.Filter.Eq(user => user.Nic, nic),
            Builders<User>.Filter.Eq(user => user.Email, email));

        return _users.Find(filter).AnyAsync(cancellationToken);
    }

    public Task CreateAsync(User user, CancellationToken cancellationToken = default)
    {
        return _users.InsertOneAsync(user, cancellationToken: cancellationToken);
    }

    public async Task<IReadOnlyList<User>> GetAsync(string? search, CancellationToken cancellationToken = default)
    {
        var filter = string.IsNullOrWhiteSpace(search)
            ? Builders<User>.Filter.Empty
            : Builders<User>.Filter.Or(
                Builders<User>.Filter.Regex(user => user.Nic, new MongoDB.Bson.BsonRegularExpression(search, "i")),
                Builders<User>.Filter.Regex(user => user.FullName, new MongoDB.Bson.BsonRegularExpression(search, "i")),
                Builders<User>.Filter.Regex(user => user.Email, new MongoDB.Bson.BsonRegularExpression(search, "i")));

        return await _users.Find(filter).SortByDescending(user => user.CreatedAt).ToListAsync(cancellationToken);
    }

    public async Task<User?> GetByNicAsync(string nic, CancellationToken cancellationToken = default)
    {
        return await _users.Find(user => user.Nic == nic).FirstOrDefaultAsync(cancellationToken);
    }

    public async Task<bool> UpdateAsync(User user, CancellationToken cancellationToken = default)
    {
        var result = await _users.ReplaceOneAsync(existing => existing.Nic == user.Nic, user, cancellationToken: cancellationToken);
        return result.ModifiedCount > 0;
    }

    public async Task<User?> FindByIdentifierAsync(string identifier, CancellationToken cancellationToken = default)
    {
        var filter = Builders<User>.Filter.Or(
            Builders<User>.Filter.Eq(user => user.Nic, identifier),
            Builders<User>.Filter.Eq(user => user.Email, identifier.ToLowerInvariant()));

        return await _users.Find(filter).FirstOrDefaultAsync(cancellationToken);
    }

    public Task<User?> FindByNicOrEmailAsync(string identifier, CancellationToken cancellationToken = default)
    {
        return FindByIdentifierAsync(identifier, cancellationToken);
    }
}
