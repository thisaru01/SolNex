using MongoDB.Driver;
using SolNex.Api.Data;
using SolNex.Api.Models;
using MongoDB.Bson;

namespace SolNex.Api.Repositories.Reservations;

public class EnergyReservationRepository : IEnergyReservationRepository
{
    private readonly IMongoCollection<EnergyReservation> _reservations;

    public EnergyReservationRepository(MongoDbContext dbContext)
    {
        _reservations = dbContext.EnergyReservations;

        // Ensure indexes
        var indexOptions = new CreateIndexOptions { Unique = true };
        var indexKeys = Builders<EnergyReservation>.IndexKeys.Ascending(r => r.ReservationId);
        var indexModel = new CreateIndexModel<EnergyReservation>(indexKeys, indexOptions);
        _reservations.Indexes.CreateOne(indexModel);
    }

    public async Task<IEnumerable<EnergyReservation>> GetAllReservationsAsync()
    {
        return await _reservations.Find(_ => true).ToListAsync();
    }

    public async Task<IEnumerable<EnergyReservation>> GetReservationsByNicAsync(string nic)
    {
        return await _reservations.Find(r => r.Nic == nic).ToListAsync();
    }

    public async Task<IEnumerable<EnergyReservation>> GetPendingReservationsAsync()
    {
        return await _reservations.Find(r => r.Status == ReservationStatus.Pending).ToListAsync();
    }

    public async Task<EnergyReservation?> GetReservationByIdAsync(string id)
    {
        if (!ObjectId.TryParse(id, out _)) return null;
        return await _reservations.Find(r => r.Id == id).FirstOrDefaultAsync();
    }

    public async Task<EnergyReservation?> GetReservationByReservationIdAsync(string reservationId)
    {
        return await _reservations.Find(r => r.ReservationId == reservationId).FirstOrDefaultAsync();
    }

    public async Task CreateReservationAsync(EnergyReservation reservation)
    {
        await _reservations.InsertOneAsync(reservation);
    }

    public async Task UpdateReservationAsync(string id, EnergyReservation reservation)
    {
        await _reservations.ReplaceOneAsync(r => r.Id == id, reservation);
    }

    public async Task DeleteReservationAsync(string id)
    {
        await _reservations.DeleteOneAsync(r => r.Id == id);
    }

    public async Task<IEnumerable<EnergyReservation>> SearchReservationsAsync(string? stationId, string? status)
    {
        var filterBuilder = Builders<EnergyReservation>.Filter;
        var filter = filterBuilder.Empty;

        if (!string.IsNullOrEmpty(stationId))
        {
            filter &= filterBuilder.Eq(r => r.StationId, stationId);
        }

        if (!string.IsNullOrEmpty(status) && Enum.TryParse<ReservationStatus>(status, true, out var parsedStatus))
        {
            filter &= filterBuilder.Eq(r => r.Status, parsedStatus);
        }

        return await _reservations.Find(filter).ToListAsync();
    }
}
