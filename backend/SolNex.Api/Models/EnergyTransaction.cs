using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace SolNex.Api.Models;

public class EnergyTransaction
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string? Id { get; set; }

    [BsonElement("transactionId")]
    public string TransactionId { get; set; } = null!;

    [BsonElement("reservationId")]
    public string ReservationId { get; set; } = null!;

    [BsonElement("nic")]
    public string Nic { get; set; } = null!;

    [BsonElement("stationId")]
    public string StationId { get; set; } = null!;

    [BsonElement("energyAmountKwh")]
    public double EnergyAmountKwh { get; set; }

    [BsonElement("qrToken")]
    public string? QrToken { get; set; }

    [BsonElement("status")]
    [BsonRepresentation(BsonType.String)]
    public TransactionStatus Status { get; set; } = TransactionStatus.Pending;

    [BsonElement("verifiedAt")]
    public DateTime? VerifiedAt { get; set; }

    [BsonElement("completedAt")]
    public DateTime? CompletedAt { get; set; }

    [BsonElement("operatorNic")]
    public string? OperatorNic { get; set; }
}

public enum TransactionStatus
{
    Pending,
    Verified,
    Completed,
    Failed
}
