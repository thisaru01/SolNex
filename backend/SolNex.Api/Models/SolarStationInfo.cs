using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace SolNex.Api.Models;

public class SolarStationInfo
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    [BsonElement("stationId")]
    public string StationId { get; set; } = null!;

    [BsonElement("stationName")]
    public string StationName { get; set; } = null!;

    [BsonElement("latitude")]
    public double Latitude { get; set; }

    [BsonElement("longitude")]
    public double Longitude { get; set; }

    [BsonElement("capacityKw")]
    public double CapacityKw { get; set; }

    [BsonElement("totalBatterySlots")]
    public int TotalBatterySlots { get; set; }

    [BsonElement("availableBatterySlots")]
    public int AvailableBatterySlots { get; set; }

    [BsonElement("status")]
    [BsonRepresentation(BsonType.String)]
    public StationStatus Status { get; set; } = StationStatus.Active;

    [BsonElement("schedule")]
    public Dictionary<string, string>? Schedule { get; set; }

    [BsonElement("createdAt")]
    public DateTime CreatedAt { get; set; }

    [BsonElement("updatedAt")]
    public DateTime UpdatedAt { get; set; }
}

public enum StationStatus
{
    Active,
    Inactive,
    Maintenance
}
