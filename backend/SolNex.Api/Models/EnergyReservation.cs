using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace SolNex.Api.Models;

public class EnergyReservation
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    [BsonElement("reservationId")]
    public string ReservationId { get; set; } = null!;

    [BsonElement("nic")]
    public string Nic { get; set; } = null!;

    [BsonElement("stationId")]
    public string StationId { get; set; } = null!;

    [BsonElement("slotId")]
    public string SlotId { get; set; } = null!;

    [BsonElement("reservationDate")]
    public DateTime ReservationDate { get; set; }

    [BsonElement("energyAmountKwh")]
    public double EnergyAmountKwh { get; set; }

    [BsonElement("status")]
    [BsonRepresentation(BsonType.String)]
    public ReservationStatus Status { get; set; } = ReservationStatus.Pending;

    [BsonElement("approvedBy")]
    public string? ApprovedBy { get; set; } // operator NIC

    [BsonElement("approvedAt")]
    public DateTime? ApprovedAt { get; set; }

    [BsonElement("rejectedReason")]
    public string? RejectedReason { get; set; }

    [BsonElement("createdAt")]
    public DateTime CreatedAt { get; set; }

    [BsonElement("updatedAt")]
    public DateTime UpdatedAt { get; set; }
}

public enum ReservationStatus
{
    Pending,
    Approved,
    Rejected,
    Cancelled,
    Completed
}
