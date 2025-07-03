/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

import koneksi.KoneksiDB;
import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 *
 * @author Lenovo
 */
public class FormObat extends JPanel{
    private JTextField tfNama, tfPembuat, tfStok, tfTanggal;
    private JButton btnSimpan, btnLihat, btnReset;
    private JTable table;
    private DefaultTableModel model;
    
    public FormObat() {
        setLayout(new BorderLayout());

        // Panel input
        JPanel panelInput = new JPanel(new GridLayout(5, 2, 5, 5));       
        tfNama = new JTextField();
        tfNama.addKeyListener(new KeyAdapter(){
            public void keyTyped(KeyEvent e){
                char c = e.getKeyChar();
                if (Character.isDigit(c)){
                    e.consume(); //blokir angka
                }
            }
        });         
        tfPembuat = new JTextField();
        tfPembuat.addKeyListener(new KeyAdapter(){
            public void keyTyped(KeyEvent e){
                char c = e.getKeyChar();
                if (Character.isDigit(c)){
                    e.consume(); //blokir angka
                }
            }
        });
        tfTanggal = new JTextField();
        tfStok = new JTextField();
        tfStok.addKeyListener(new KeyAdapter(){
            public void keyTyped(KeyEvent e){
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && !Character.isISOControl(c)){
                    e.consume(); //blokir input selain angka
                }
            }
        });
        btnSimpan = new JButton("Simpan");
        btnLihat = new JButton("Lihat Data");
        btnReset = new JButton("Reset");

        panelInput.setBorder(BorderFactory.createTitledBorder("Form Input Obat"));
        panelInput.add(new JLabel("Nama:")); panelInput.add(tfNama);
        panelInput.add(new JLabel("Pembuat:")); panelInput.add(tfPembuat);
        panelInput.add(new JLabel("Stok:")); panelInput.add(tfStok);
        panelInput.add(new JLabel("Tanggal Kadaluwarsa (YYYY-MM-DD):")); panelInput.add(tfTanggal);

        // Panel tombol (terpisah)
        JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelButton.add(btnSimpan);
        panelButton.add(btnLihat);
        panelButton.add(btnReset);

        // Tabel
        model = new DefaultTableModel(new String[]{"ID", "Nama", "Pembuat", "Stok", "Tanggal Kadaluwarsa", "Aksi"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // hanya kolom aksi
            }
        };
        table = new JTable(model);
        table.setRowHeight(40);

        // Tambah ButtonRenderer & Editor
        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(table, new AksiEditHapus() {
            @Override
            public void onEdit(int row) {
                tfNama.setText(model.getValueAt(row, 1).toString());
                tfPembuat.setText(model.getValueAt(row, 2).toString());
                tfStok.setText(model.getValueAt(row, 3).toString());
                tfTanggal.setText(model.getValueAt(row, 4).toString());
            }

            @Override
            public void onHapus(int id) {
                try (Connection conn = KoneksiDB.getConnection()) {
                    int konfirmasi = JOptionPane.showConfirmDialog(null, "Yakin hapus data ini?", "Hapus", JOptionPane.YES_NO_OPTION);
                    if (konfirmasi == JOptionPane.YES_OPTION) {
                        conn.createStatement().executeUpdate("DELETE FROM obat WHERE id=" + id);
                        tampilkanData();
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Gagal hapus: " + e.getMessage());
                }
            }
        }));

        JScrollPane scrollPane = new JScrollPane(table);

        // Action
        btnSimpan.addActionListener(e -> simpanData());
        btnLihat.addActionListener(e -> tampilkanData());
        btnReset.addActionListener(e -> resetData());

        // Gabungkan panel input dan tombol ke panel atas
        JPanel panelAtas = new JPanel(new BorderLayout());
        panelAtas.add(panelInput, BorderLayout.CENTER);
        panelAtas.add(panelButton, BorderLayout.SOUTH);

        add(panelAtas, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void simpanData() {
        try {
            Connection conn = KoneksiDB.getConnection();
            String sql = "INSERT INTO obat (nama_obat, pembuat_obat, stok_obat, tgl_kadaluwarsa) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, tfNama.getText());
            stmt.setString(2, tfPembuat.getText());
            stmt.setString(3, tfStok.getText());
            stmt.setString(4, tfTanggal.getText());
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Data obat berhasil disimpan.");
            resetData();
            tampilkanData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data: " + ex.getMessage());
        }
    }

    private void tampilkanData() {
        model.setRowCount(0);
        try {
            Connection conn = KoneksiDB.getConnection();
            String sql = "SELECT * FROM obat";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nama_obat"),
                    rs.getString("pembuat_obat"),
                    rs.getString("stok_obat"),
                    rs.getString("tgl_kadaluwarsa"),
                    "Aksi"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menampilkan data: " + ex.getMessage());
        }
    }

    private void resetData() {
        tfNama.setText("");
        tfPembuat.setText("");
        tfStok.setText("");
        tfTanggal.setText("");
    }
}
    

