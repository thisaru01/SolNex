using MongoDB.Driver;
using SolNex.Api.Data;
using SolNex.Api.Models;

namespace SolNex.Api.Repositories;

/// <summary>
/// Repository for user data access operations.
/// Handles CRUD operations for user entities in MongoDB.
/// </summary>
public sealed partial class UserRepository : IUserRepository
{
    private readonly IMongoCollection<User> _users;

    public UserRepository(MongoDbContext dbContext)
    {
        _users = dbContext.Users;
    }

    // Check if user exists by NIC or email
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

    // Check if email exists for a different user (excluding current NIC)
    public Task<bool> ExistsByEmailExceptNicAsync(
        string email,
        string nic,
        CancellationToken cancellationToken = default)
    {
        var filter = Builders<User>.Filter.And(
            Builders<User>.Filter.Eq(user => user.Email, email),
            Builders<User>.Filter.Ne(user => user.Nic, nic));

        return _users.Find(filter).AnyAsync(cancellationToken);
    }

    // Create a new user
    public Task CreateAsync(User user, CancellationToken cancellationToken = default)
    {
        return _users.InsertOneAsync(user, cancellationToken: cancellationToken);
    }

    // Get users with optional search filter
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

    // Get user by NIC
    public async Task<User?> GetByNicAsync(string nic, CancellationToken cancellationToken = default)
    {
        return await _users.Find(user => user.Nic == nic).FirstOrDefaultAsync(cancellationToken);
    }

    // Update user information
    public async Task<bool> UpdateAsync(User user, CancellationToken cancellationToken = default)
    {
        var result = await _users.ReplaceOneAsync(existing => existing.Nic == user.Nic, user, cancellationToken: cancellationToken);
        return result.ModifiedCount > 0;
    }

    // Find user by NIC or email identifier
    public async Task<User?> FindByIdentifierAsync(string identifier, CancellationToken cancellationToken = default)
    {
        var filter = Builders<User>.Filter.Or(
            Builders<User>.Filter.Eq(user => user.Nic, identifier),
            Builders<User>.Filter.Eq(user => user.Email, identifier.ToLowerInvariant()));

        return await _users.Find(filter).FirstOrDefaultAsync(cancellationToken);
    }

    // Find user by NIC or email (alias for FindByIdentifierAsync)
    public Task<User?> FindByNicOrEmailAsync(string identifier, CancellationToken cancellationToken = default)
    {
        return FindByIdentifierAsync(identifier, cancellationToken);
    }
}
