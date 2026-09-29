/*
 * File: ReservationApprovalRepository.cs
 * Component: Transaction Repositories
 * Description:
 * Implements MongoDB persistence operations for reservation approvals.
 */

using MongoDB.Bson;
using MongoDB.Driver;
using SolNex.Api.Data;
using SolNex.Api.Models;

namespace SolNex.Api.Repositories.Transactions;

public class ReservationApprovalRepository : IReservationApprovalRepository
{
    private readonly IMongoCollection<EnergyReservation> _reservations;

    public ReservationApprovalRepository(MongoDbContext dbContext)
    {
        // Selects the reservation collection from the shared MongoDB context.
        _reservations = dbContext.EnergyReservations;
    }

    public async Task<EnergyReservation?> GetByIdOrReservationIdAsync(string id)
    {
        // Finds a reservation using either its MongoDB ID or public reservation ID.
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

    public async Task<IEnumerable<EnergyReservation>> GetPendingForSlotDateAsync(string slotId, DateTime reservationDate)
    {
        // Retrieves pending reservations for the same slot and calendar date.
        var start = reservationDate.Date;
        var end = start.AddDays(1);

        var filter = Builders<EnergyReservation>.Filter.And(
            Builders<EnergyReservation>.Filter.Eq(r => r.SlotId, slotId),
            Builders<EnergyReservation>.Filter.Eq(r => r.Status, ReservationStatus.Pending),
            Builders<EnergyReservation>.Filter.Gte(r => r.ReservationDate, start),
            Builders<EnergyReservation>.Filter.Lt(r => r.ReservationDate, end));

        return await _reservations.Find(filter).ToListAsync();
    }

    public async Task UpdateAsync(EnergyReservation reservation)
    {
        // Replaces the persisted reservation with its updated state.
        if (string.IsNullOrWhiteSpace(reservation.Id))
        {
            throw new InvalidOperationException("Reservation database ID is missing.");
        }

        await _reservations.ReplaceOneAsync(r => r.Id == reservation.Id, reservation);
    }
}