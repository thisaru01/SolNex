using MongoDB.Bson;
using MongoDB.Driver;
using SolNex.Api.Data;
using SolNex.Api.Models;

namespace SolNex.Api.Repositories.Transactions;

public class ReservationApprovalRepository : IReservationApprovalRepository
{
    private readonly IMongoCollection<EnergyReservation> _reservations;

    // Receives the shared MongoDB context and selects the reservation collection.
    public ReservationApprovalRepository(MongoDbContext dbContext)
    {
        _reservations = dbContext.EnergyReservations;
    }

    // Finds a reservation using either its MongoDB object ID or public reservation ID.
    public async Task<EnergyReservation?> GetByIdOrReservationIdAsync(string id)
    {
        if (ObjectId.TryParse(id, out _))
        {
            var byObjectId = await _reservations.Find(r => r.Id == id).FirstOrDefaultAsync();
            if (byObjectId != null)
            {
                return byObjectId;
            }
        }

        return await _reservations.Find(r => r.ReservationId == id).FirstOrDefaultAsync();
    }

    // Retrieves other pending reservations for the same slot and calendar date.
    public async Task<IEnumerable<EnergyReservation>> GetPendingForSlotDateAsync(string slotId, DateTime reservationDate)
    {
        var start = reservationDate.Date;
        var end = start.AddDays(1);

        var filter = Builders<EnergyReservation>.Filter.And(
            Builders<EnergyReservation>.Filter.Eq(r => r.SlotId, slotId),
            Builders<EnergyReservation>.Filter.Eq(r => r.Status, ReservationStatus.Pending),
            Builders<EnergyReservation>.Filter.Gte(r => r.ReservationDate, start),
            Builders<EnergyReservation>.Filter.Lt(r => r.ReservationDate, end));

        return await _reservations.Find(filter).ToListAsync();
    }

    // Replaces the persisted reservation with its updated state.
    public async Task UpdateAsync(EnergyReservation reservation)
    {
        if (string.IsNullOrWhiteSpace(reservation.Id))
        {
            throw new InvalidOperationException("Reservation database ID is missing.");
        }

        await _reservations.ReplaceOneAsync(r => r.Id == reservation.Id, reservation);
    }
}