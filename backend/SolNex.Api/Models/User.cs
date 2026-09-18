using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace SolNex.Api.Models;

public class User
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    [BsonElement("nic")]
    public string Nic { get; set; } = null!;

    [BsonElement("fullName")]
    public string FullName { get; set; } = null!;

    [BsonElement("email")]
    public string Email { get; set; } = null!;

    [BsonElement("phone")]
    public string Phone { get; set; } = null!;

    [BsonElement("passwordHash")]
    public string PasswordHash { get; set; } = null!;

    [BsonElement("role")]
    [BsonRepresentation(BsonType.String)]
    public UserRole Role { get; set; } = UserRole.Prosumer;

    [BsonElement("accountStatus")]
    [BsonRepresentation(BsonType.String)]
    public AccountStatus AccountStatus { get; set; } = AccountStatus.Pending;

    [BsonElement("createdAt")]
    public DateTime CreatedAt { get; set; }

    [BsonElement("updatedAt")]
    public DateTime UpdatedAt { get; set; }
}

public enum UserRole
{
    Backoffice,
    GridOperator,
    Prosumer
}

public enum AccountStatus
{
    Pending,
    Active,
    Inactive
}
