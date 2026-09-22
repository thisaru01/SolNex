using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace SolNex.Api.Models;

public class EnergyBookingSlot
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    [BsonElement("slotId")]
    public string SlotId { get; set; } = null!;

    [BsonElement("stationId")]
    public string StationId { get; set; } = null!;


    [BsonElement("startTime")]
    public string StartTime { get; set; } = null!;

    [BsonElement("endTime")]
    public string EndTime { get; set; } = null!;

    [BsonElement("dayOfWeek")]
    public string? DayOfWeek { get; set; }

    [BsonElement("scheduleTime")]
    public string? ScheduleTime { get; set; }

    [BsonElement("slotStatus")]
    [BsonRepresentation(BsonType.String)]
    public SlotStatus SlotStatus { get; set; } = SlotStatus.Available;

    [BsonElement("createdAt")]
    public DateTime CreatedAt { get; set; }

    [BsonElement("updatedAt")]
    public DateTime UpdatedAt { get; set; }
}

public enum SlotStatus
{
    Available,
    Reserved,
    Unavailable,
    Completed
}
