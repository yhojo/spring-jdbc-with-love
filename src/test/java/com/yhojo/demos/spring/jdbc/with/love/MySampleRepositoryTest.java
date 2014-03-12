package com.yhojo.demos.spring.jdbc.with.love;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "./MySampleRepositoryTest.xml")
public class MySampleRepositoryTest {
	@Autowired
	private MySampleRepository sampleRepository;

	@Test
	public void test() {
		MyPerson person = new MyPerson();
		person.id = 1;
		person.name = "Yasuo Hojo";
		person.age = 12;
		person.setRole("Java Programmer");
		sampleRepository.insert(person);
		MyPerson anotherPerson = new MyPerson();
		anotherPerson.id = 2;
		anotherPerson.name = "Another Name";
		anotherPerson.age = 31;
		anotherPerson.setRole("Ruby Programmer");
		sampleRepository.insert(anotherPerson);

		// id=1を検索して内容が合致しているか確認
		MyPerson foundPerson = sampleRepository.findById(1);
		assertEquals(person.id, foundPerson.id);
		assertEquals(person.name, foundPerson.name);
		assertEquals(person.age, foundPerson.age);
		assertEquals(person.getRole(), foundPerson.getRole());

		// 全部検索してみる。
		List<MyPerson> persons = sampleRepository.findAll();
		assertEquals(2, persons.size());
		assertEquals(person.name, persons.get(0).name);
		assertEquals(anotherPerson.name, persons.get(1).name);

		// id=1を更新する
		person.age = 43;
		sampleRepository.update(person);
		foundPerson = sampleRepository.findById(1);
		assertEquals(person.id, foundPerson.id);
		assertEquals(person.name, foundPerson.name);
		assertThat(foundPerson.age, is(43));

		// id=1を削除する
		int rtc = sampleRepository.delete(person);
		assertEquals(rtc, 1);
		foundPerson = sampleRepository.findById(1);
		assertNull(foundPerson);
		
		// id=1を削除してもid2は存在する。
		foundPerson = sampleRepository.findById(2);
		assertEquals(anotherPerson.name, foundPerson.name);

		// id=2も削除する。
		sampleRepository.delete(anotherPerson);
		foundPerson = sampleRepository.findById(2);
		assertNull(foundPerson);
	}
}
